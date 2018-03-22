package ru.apertum.qsystem.prereg;

import java.util.Map;
import org.zkoss.bind.Property;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;

public class ClientOneFormValidator extends AbstractValidator {
    
    public String l(String resName) {
        return Labels.getLabel(resName);
    }

    @Override
    public void validate(ValidationContext ctx) {
        //all the bean properties
        Map<String, Property> beanProps = ctx.getProperties(ctx.getProperty().getBase());
        validateCaptcha(ctx, (String) ctx.getValidatorArg("captcha"), (String) ctx.getValidatorArg("captchaInput"));
    }

    private void validateCaptcha(ValidationContext ctx, String captcha, String captchaInput) {
        if (captchaInput == null || !captcha.equals(captchaInput)) {
            this.addInvalidMessage(ctx, "captcha", l("bad_captcha"));
        }
    }
}
