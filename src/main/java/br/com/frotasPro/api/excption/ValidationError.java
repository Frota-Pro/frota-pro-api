package br.com.frotasPro.api.excption;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ValidationError extends CustomException{

    private List<FieldMessage> errors = new ArrayList<>();

    public ValidationError(Instant timestamp, Integer status, String error, String path) {
        super(timestamp, status, error, path);
    }

    public List<FieldMessage> getErrors() {
        return errors;
    }

    public void addError(String fieldname, String fieldmessage){
        errors.removeIf(x -> x.getName().equals(fieldname));
        errors.add(new FieldMessage(fieldname,fieldmessage));
    }
}
