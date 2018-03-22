package ru.apertum.qsystem.prereg;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import ru.apertum.qsystem.prereg.core.ServiceList;

public class ClientForm extends Index {

    private String com;   

    @Init
    @Override
    public void init() {
        super.init();
    }

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);
        Selectors.wireEventListeners(view, this);

    }

    private final RandomStringGenerator rsg = new RandomStringGenerator(5);
    private Client client = new Client();
    private String captcha = rsg.getRandomString(), captchaInput;

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public Client getClient() {
        final Client cl = (Client) Sessions.getCurrent().getAttribute("DATA");
        if (cl != null) {
            client = cl;
        }
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getCaptchaInput() {
        return captchaInput;
    }

    public void setCaptchaInput(String captchaInput) {
        this.captchaInput = captchaInput;
    }

    public void regenerateCaptcha() {
        this.captcha = rsg.getRandomString();
    }
    private String foregroundColour = "#000000", backgroundColour = "#FDC966";

    public String getForegroundColour() {
        return foregroundColour;
    }

    public void setForegroundColour(String foregroundColor) {
        this.foregroundColour = foregroundColor;
    }

    public String getBackgroundColour() {
        return backgroundColour;
    }

    public void setBackgroundColour(String backgroundColor) {
        this.backgroundColour = backgroundColor;
    }

    @Command
    @NotifyChange("captcha")
    public void regenerate() {
        this.regenerateCaptcha();
    }

    @Command
    public void submit() {
        try {
            ServiceList.getInstance().getServiceList();
            Sessions.getCurrent().setAttribute("DATA", client);
            //Executions.sendRedirect("/selectService");
            Executions.sendRedirect("/selectService.zul");
        } catch (Throwable t) {
            System.err.println("Server QMS is down! " + t);
            Executions.sendRedirect("/error.zul");
        }
    }
}
