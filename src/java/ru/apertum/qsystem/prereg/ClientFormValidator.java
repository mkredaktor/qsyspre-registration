package ru.apertum.qsystem.prereg;

import java.util.Map;
import org.zkoss.bind.Property;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.WrongValueException;

public class ClientFormValidator extends AbstractValidator {
    
    public String l(String resName) {
        return Labels.getLabel(resName);
    }

    @Override
    public void validate(ValidationContext ctx) {
        //all the bean properties
        Map<String, Property> beanProps = ctx.getProperties(ctx.getProperty().getBase());

        validateName(ctx, (String) beanProps.get("name").getValue());
        validateSourname(ctx, (String) beanProps.get("sourname").getValue());
        //validateMiddlename(ctx, (String) beanProps.get("middlename").getValue());
        validateEmail(ctx, (String) beanProps.get("email").getValue());

        validateCaptcha(ctx, (String) ctx.getValidatorArg("captcha"), (String) ctx.getValidatorArg("captchaInput"));
        
    }

    private void validateEmail(ValidationContext ctx, String email) {
        if (email != null && !email.isEmpty() && !email.matches(".+@.+\\.[a-z]+")) {
           this.addInvalidMessage(ctx, "email", l("bad_email"));
        }
    }

    private void validateCaptcha(ValidationContext ctx, String captcha, String captchaInput) {
        if (captchaInput == null || !captcha.equals(captchaInput)) {
            this.addInvalidMessage(ctx, "captcha", l("bad_captcha"));
        }
    }

    private void validateMiddlename(ValidationContext ctx, String string) {
        if (string == null || string.isEmpty() || string.length() < 3) {
            this.addInvalidMessage(ctx, "middlename", l("put_middlename"));
        }
    }

    private void validateName(ValidationContext ctx, String string) {
        if (string == null || string.isEmpty() || string.length() < 2) {
            this.addInvalidMessage(ctx, "name", l("put_name"));
        }
    }

    private void validateSourname(ValidationContext ctx, String string) {
        if (string == null || string.isEmpty() || string.length() < 2) {
            //this.addInvalidMessage(ctx, "sourname", l("put_surname"));
                    throw new WrongValueException(ctx.getBindContext().getComponent(), l("put_surname"));
        }
    }
}
