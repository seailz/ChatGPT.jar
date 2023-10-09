package lol.slz.gptjar.request;

import lol.slz.gptjar.ChatGPT;
import lol.slz.gptjar.model.Function;
import lol.slz.gptjar.model.Message;
import lol.slz.gptjar.util.Request;
import lol.slz.gptjar.util.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import java.util.ArrayList;
import java.util.List;

public class ChatCompletionRequest {

    private List<Message> messages = new ArrayList<>();
    private String model;
    private List<Function> functions = new ArrayList<>();
    private String functionCall;
    private double temperature;
    private double topP;
    private int n;
    private int maxTokens;
    private ChatGPT chatGPT;

    public ChatCompletionRequest(List<Message> messages, String model, ChatGPT gpt) {
        this.messages = messages;
        this.model = model;
        this.chatGPT = gpt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public void setFunctions(List<Function> functions) {
        this.functions = functions;
    }

    public void addFunction(Function function) {
        functions.add(function);
    }

    public String getFunctionCall() {
        return functionCall;
    }

    public void setFunctionCall(String functionCall) {
        this.functionCall = functionCall;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getTopP() {
        return topP;
    }

    public void setTopP(double topP) {
        this.topP = topP;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public JSONObject toJson() {
        JSONArray messagesJson = new JSONArray();
        for (Message message : messages) {
            messagesJson.put(message.toJson());
        }

        JSONArray functionsJson = new JSONArray();
        for (Function function : functions) {
            functionsJson.put(function.toJson());
        }

        JSONObject json = new JSONObject();
        json.put("messages", messagesJson);
        json.put("model", model);
        if (!functionsJson.isEmpty()) {
            json.put("functions", functionsJson);
        }
        if (functionCall != null) {
            if (!functionCall.equals("none") && !functionCall.equals("auto")) {
                json.put("function_call", new JSONObject().put("name", functionCall));
            } else json.put("function_call", functionCall);
        }

        if (temperature != 0.0) {
            json.put("temperature", temperature);
        }

        if (topP != 0.0) {
            json.put("top_p", topP);
        }

        if (n != 0) {
            json.put("n", n);
        }

        if (maxTokens != 0) {
            json.put("max_tokens", maxTokens);
        }

        return json;
    }

    public Response<String> call() {
        Response<String> response = new Response<>();
        // We need to call it normally first, then check if the model wanted to call a function, and then call that function, then send a new request with the function's content.
        new Thread(() -> {
            Request firstRequest = new Request(new Request.Path("https://api.openai.com/v1/chat/completions", new String[]{}, "POST"), toJson(), chatGPT);
            Request.OpenAiApiResponse firstResponse = firstRequest.send();
            if (firstResponse.error()) {
                response.completeError(new Response.Error(
                        firstResponse.errorCode(),
                        new JSONObject(firstResponse.body()
                )));
            } else {
                JSONObject firstResponseJson = new JSONObject(firstResponse.body());
                JSONArray choices = firstResponseJson.getJSONArray("choices");
                JSONObject choice = choices.getJSONObject(0);
                JSONObject msg = choice.getJSONObject("message");
                if (!msg.has("function_call") || msg.isNull("function_call"))  {
                    // Complete normally
                    response.complete(msg.getString("content"));
                } else {
                    JSONObject functionCall = msg.getJSONObject("function_call");
                    String functionName = functionCall.getString("name");
                    String args = functionCall.getString("arguments");
                    args = args.replace("\n", "");
                    args = args.replace("\\", "");
                    JSONObject argsJson = new JSONObject(args);
                    HashMap<String, Object> argsMap = new HashMap<>();
                    for (String key : argsJson.keySet()) {
                        argsMap.put(key, argsJson.get(key));
                    }

                    Function function = null;
                    for (Function f : functions) {
                        if (f.getName().equals(functionName)) {
                            function = f;
                            break;
                        }
                    }

                    if (function == null) {
                        response.completeError(new Response.Error(
                                0,
                                new JSONObject().put("error", "Function not found")
                        ));
                    } else {
                        HashMap<String, Object> res = function.getOnCall().apply(
                                argsMap
                        );

                        // Send a new request with the function's content
                        // Check if messages is immutable, if so, create a new list
                        if (!(messages instanceof ArrayList)) {
                            messages = new ArrayList<>(messages);
                        }

                        messages.add(new Message(Message.Role.FUNCTION, functionName, res));

                        Response<String> secondResponse = call();
                        secondResponse.onCompletion((s) -> response.complete(secondResponse.response));
                    }
                }
            }
        }).start();
        return response;
    }
}
