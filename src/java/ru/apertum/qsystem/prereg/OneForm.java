package ru.apertum.qsystem.prereg;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TreeSet;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Combobox;
import ru.apertum.qsystem.common.NetCommander;
import ru.apertum.qsystem.common.cmd.RpcGetGridOfWeek;
import ru.apertum.qsystem.common.model.QAdvanceCustomer;
import ru.apertum.qsystem.prereg.core.MailSender;
import ru.apertum.qsystem.prereg.core.ServiceList;
import ru.apertum.qsystem.prereg.core.TreeServices;
import ru.apertum.qsystem.server.model.QService;

public class OneForm extends Index {

    private String com;

    @Init
    @Override
    public void init() {
        super.init();
        final Session sess = Sessions.getCurrent();
        SiteProperty sp = (ru.apertum.qsystem.prereg.SiteProperty) sess.getAttribute("PROPS");
        String com = Executions.getCurrent().getParameter("com");
        if (sp == null || com != null) {
            sp = new ru.apertum.qsystem.prereg.SiteProperty(com,
                    System.getProperty(com + "_" + "QSYSPREREG_TITLE"),
                    System.getProperty(com + "_" + "QSYSPREREG_CAPTION"),
                    System.getProperty(com + "_" + "QSYSPREREG_LOGO"),
                    System.getProperty(com + "_" + "QSYSPREREG_PASSWORD"),
                    System.getProperty(com + "_" + "QSYSTEM_SERVER_ADDR"),
                    Integer.parseInt(System.getProperty(com + "_" + "QSYSTEM_SERVER_PORT", "3128")),
                    System.getProperty(com + "_" + "QSYSPREREG_MAIL_CONTENT")
            );
            Sessions.getCurrent().setAttribute("PROPS", sp);
        }

        treeServs = new TreeServices(ServiceList.getInstance().getRoot());
        done = false;
        fail = false;

        Client cl = (Client) sess.getAttribute("DATA");
        if (cl != null) {
            client = cl;
            done = true;
            pickedRedirectServ = cl.getService();
        }
    }

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);
        Selectors.wireEventListeners(view, this);
    }

    private final RandomStringGenerator rsg = new RandomStringGenerator(5);
    private String captcha = rsg.getRandomString(), captchaInput;

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
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

    public boolean getInputDataRequired() {
        return pickedRedirectServ != null && pickedRedirectServ.getInput_required();
    }

    public boolean getPreInfoHtmtRequired() {
        return pickedRedirectServ != null && pickedRedirectServ.getPreInfoHtml() != null && !pickedRedirectServ.getPreInfoHtml().isEmpty();
    }

    public boolean getPicked() {
        return pickedRedirectServ != null && !getDone() && !getFail();
    }

    private boolean done = false;

    public boolean getDone() {
        return done;
    }

    private boolean fail = false;

    public boolean getFail() {
        return fail;
    }

    //********************************************************************************************************************************************
    //** Древовидный комбобокс
    //********************************************************************************************************************************************
    private TreeServices treeServs;

    public TreeServices getTreeServs() {
        return treeServs;
    }

    private QService pickedRedirectServ;

    public QService getPickedRedirectServ() {
        return pickedRedirectServ;
    }

    @NotifyChange({"timeList", "inputDataRequired", "preInfoHtmtRequired", "pickedRedirectServ", "client", "picked", "date", "selectedDate"})
    public void setPickedRedirectServ(QService pickedRedirectServ) {
        this.pickedRedirectServ = pickedRedirectServ;
        client.setService(pickedRedirectServ);
        pickedRedirectServ.setInput_caption(pickedRedirectServ.getInput_caption().replaceAll("<.*?>", ""));
    }

    @Wire("#cbTime")
    private Combobox cbTime;

    private Date selectedDate;

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }
    private Date date = new Date();

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    private final TreeSet<Date> timeList = new TreeSet<>();

    public TreeSet<Date> getTimeList() {
        return timeList;
    }

    @Command
    @NotifyChange({"timeList", "selectedDate"})
    public void changeDate() {
        RpcGetGridOfWeek.GridAndParams gp;
        try {
            gp = NetCommander.getGridOfWeek(ServiceList.netProperty, pickedRedirectServ.getId(), date, -1);
        } catch (Exception ex) {
            throw new RuntimeException("Bad net conversation: " + ex);
        }
        timeList.clear();
        selectedDate = null;
        cbTime.setSelectedIndex(-1);
        if (gp.getSpError() == null) {
            final GregorianCalendar gcSt = new GregorianCalendar();
            gcSt.setTime(gp.getStartTime());
            final GregorianCalendar gcFin = new GregorianCalendar();
            gcFin.setTime(gp.getFinishTime());

            final GregorianCalendar gc = new GregorianCalendar();
            final GregorianCalendar gc1 = new GregorianCalendar();
            gc.setTime(date);
            for (Date d : gp.getTimes()) {
                gc1.setTime(d);
                if (gc.get(GregorianCalendar.DAY_OF_YEAR) == gc1.get(GregorianCalendar.DAY_OF_YEAR)
                        && gc1.get(GregorianCalendar.HOUR_OF_DAY) > gcSt.get(GregorianCalendar.HOUR_OF_DAY)
                        && gc1.get(GregorianCalendar.HOUR_OF_DAY) < gcFin.get(GregorianCalendar.HOUR_OF_DAY)) {
                    timeList.add(d);
                }
            }
        } else {
            System.out.println(gp.getSpError());
        }

    }

    private String inputData = "";

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    private Client client = new Client();

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    private String input_data_required = "";
    private String date_time_required = "";

    public String getInput_data_required() {
        return input_data_required;
    }

    public String getDate_time_required() {
        return date_time_required;
    }

    @Command
    @NotifyChange({"done", "fail", "picked", "client", "input_data_required", "date_time_required"})
    public void submit() {
        if (pickedRedirectServ.getInput_required() && (inputData == null || inputData.isEmpty())) {
            input_data_required = l("input_data_required");
            return;
        }
        input_data_required = "";

        if (selectedDate == null || date == null) {
            date_time_required = l("date_time_required");
            return;
        }
        date_time_required = "";

        if (selectedDate != null && pickedRedirectServ != null) {
            System.out.println("---- #### ---- #### ---- #### ---- #### ---- " + selectedDate);
            // ставим предварительного кастомера
            try {
                final QAdvanceCustomer result = NetCommander.standInServiceAdvance(ServiceList.netProperty, pickedRedirectServ.getId(), selectedDate, -1, getInputData());
                client.setAdvClient(result);
                client.setInputData(inputData);
                client.setDate(selectedDate);
                Sessions.getCurrent().setAttribute("DATA", client);
                done = true;
            } catch (Exception ex) {
                System.err.println("[ERROR] stand In Servic Advance was fail. " + ex);
                fail = true;
                done = false;
            }
        }
    }

    private String mailResult = l("send_mail");

    public String getMailResult() {
        return mailResult;
    }

    @Command
    @NotifyChange("mailResult")
    public void sendMail() {
        if (client.getEmail() != null && !client.getEmail().isEmpty()) {
            try {
                MailSender.getInstance().sendMessage(client);
                mailResult = l("successfully");
            } catch (Exception ex) {
                mailResult = l("failure");
                System.err.println("[ERROR] Mail was not sent: " + ex);
            }
        }
    }
}
