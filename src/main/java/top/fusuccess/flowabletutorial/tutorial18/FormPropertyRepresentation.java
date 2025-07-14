package top.fusuccess.flowabletutorial.tutorial18;

import org.flowable.engine.form.FormType;

public class FormPropertyRepresentation {
    private String id;
    private String name;
    private FormType type;
    private Boolean  required;
    private String datePattern;

    private String value;

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FormType getType() {
        return type;
    }

    public void setType(FormType type) {
        this.type = type;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }
    public String getDatePattern() {
        return datePattern;
    }
}
