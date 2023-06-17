package lol.slz.gptjar.model;

import org.json.JSONObject;

import java.util.HashMap;

public class Message {
    private Role role;
    private String text;
    private String functionName;

    public Message(Role role, String text) {
        this.role = role;
        this.text = text;
    }

    public Message(Role role, String functionName, HashMap<String, Object> functionContent) {
        this.role = role;
        this.text = new JSONObject(functionContent).toString();
        this.functionName = functionName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject()
                .put("role", role.toString().toLowerCase())
                .put("content", text);
        if (functionName != null) {
            obj.put("name", functionName);
        }
        return obj;
    }

    public static Message fromJson(JSONObject json) {
        return new Message(
                Role.fromString(json.getString("role")),
                json.getString("content")
        );
    }

    public enum Role {
        USER,
        SYSTEM,
        ASSISTANT,
        FUNCTION;

        public static Role fromString(String role) {
            return switch (role) {
                case "user" -> USER;
                case "assistant" -> ASSISTANT;
                case "function" -> FUNCTION;
                default -> throw new IllegalArgumentException("Invalid role: " + role);
            };
        }
    }
}
