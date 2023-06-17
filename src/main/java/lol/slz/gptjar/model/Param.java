package lol.slz.gptjar.model;

public class Param {

    private String name;
    private String description;
    private Enum<? extends FunctionEnum> functionEnum;

    public Param(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Param(String name, String description, Enum<? extends FunctionEnum> functionEnum) {
        this.name = name;
        this.description = description;
        this.functionEnum = functionEnum;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Enum<? extends FunctionEnum> getFunctionEnum() {
        return functionEnum;
    }

}
