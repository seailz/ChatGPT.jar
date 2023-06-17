package lol.slz.gptjar.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class Function {

    private String name;
    private String description;
    // Hashmap of parameter name to parameter type
    private HashMap<Param, Class<?>> parameters = new HashMap<>();
    private List<String> requiredParameters;
    private java.util.function.Function<HashMap<String, Object>, HashMap<String, Object>> onCall = (params) -> {
        return new HashMap<>();
    };

    public Function(String name, String description, HashMap<Param, Class<?>> parameters, List<String> requiredParameters, java.util.function.Function<HashMap<String, Object>, HashMap<String, Object>> onCall) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.requiredParameters = requiredParameters;
        this.onCall = onCall;
    }

    public Function(String name,  java.util.function.Function<HashMap<String, Object>, HashMap<String, Object>> onCall) {
        this.name = name;
        this.onCall = onCall;
    }

    public Function(String name, String description,  java.util.function.Function<HashMap<String, Object>, HashMap<String, Object>> onCall) {
        this.name = name;
        this.description = description;
        this.onCall = onCall;
    }

    public Function(String name, String description, HashMap<Param, Class<?>> parameters,  java.util.function.Function<HashMap<String, Object>, HashMap<String, Object>>  onCall) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.onCall = onCall;
    }

    public Function(String name, HashMap<Param, Class<?>> parameters, java.util.function.Function<HashMap<String, Object>, HashMap<String, Object>> onCall) {
        this.name = name;
        this.parameters = parameters;
        this.onCall = onCall;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public java.util.function.Function<HashMap<String, Object>, HashMap<String, Object>> getOnCall() {
        return onCall;
    }

    public HashMap<Param, Class<?>> getParameters() {
        return parameters;
    }

    public List<String> getRequiredParameters() {
        return requiredParameters;
    }

    public JSONObject toJson() {
        JSONObject func = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("type", "object");
        JSONObject properties = new JSONObject();
        for (Param param : parameters.keySet()) {
            JSONObject property = new JSONObject();
            property.put("type", parameters.get(param).getSimpleName().toLowerCase());
            property.put("description", param.getDescription());
            if (param.getFunctionEnum() != null) {
                JSONArray enumValues = new JSONArray();
                for (Enum<?> enumValue : param.getFunctionEnum().getDeclaringClass().getEnumConstants()) {
                    enumValues.put(enumValue.name());
                }
                property.put("enum", enumValues);
            }
            properties.put(param.getName(), property);
        }
        params.put("properties", properties);
        func.put("name", name);
        if (description != null) {
            func.put("description", description);
        }

        func.put("parameters", params);

        if (requiredParameters != null && !requiredParameters.isEmpty()) {
            JSONArray required = new JSONArray();
            for (String requiredParam : requiredParameters) {
                required.put(requiredParam);
            }
            func.put("required", required);
        }

        return func;
    }
}
