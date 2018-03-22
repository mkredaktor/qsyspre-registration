package ru.apertum.qsystem.prereg;

import org.zkoss.bind.annotation.*;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.SimpleDateConstraint;
import ru.apertum.qsystem.common.NetCommander;
import ru.apertum.qsystem.common.cmd.RpcGetGridOfWeek;
import ru.apertum.qsystem.common.model.QAdvanceCustomer;
import ru.apertum.qsystem.prereg.core.MailSender;
import ru.apertum.qsystem.prereg.core.ServiceList;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TreeSet;

public class TimeForm {

    @Wire
    private Listbox listbox;

    private final TreeSet<Date> timeList = new TreeSet<>();
    private Client client = (Client) Sessions.getCurrent().getAttribute("DATA");
    private Date selectedDate;
    private Date date = getTomorrowDate();

    @Init
    public void init() {
    }

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);

        if (timeList.isEmpty())
            changeDate();
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public TreeSet<Date> getTimeList() {
        return timeList;
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    private Date getTomorrowDate() {
        // get a calendar instance, which defaults to "now"
        Calendar calendar = Calendar.getInstance();

        // get a date to represent "today"
        Date today = calendar.getTime();

        // add one day to the date/calendar
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        // now get "tomorrow"
        return calendar.getTime();
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public SimpleDateConstraint getDateConstraint() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        return new SimpleDateConstraint("after " + simpleDateFormat.format(date));
    }

    @Command
    public void submit() {
        if (selectedDate != null) {
            System.out.println("---- #### ---- #### ---- #### ---- #### ---- " + selectedDate);
            // ставим предварительного кастомера
            final QAdvanceCustomer result = NetCommander.standInServiceAdvance(ServiceList.netProperty, client.getService().getId(), selectedDate, -1, client.getInputData());
            client.setAdvClient(result);
            client.setDate(selectedDate);
            if (client.getEmail() != null && !client.getEmail().isEmpty()) {
                try {
                    MailSender.getInstance().sendMessage(client);
                } catch (Exception ex) {
                    System.err.println("[ERROR] Mail was not sent: " + ex);
                }
            }
            Executions.sendRedirect("/finish.zul");
        }
    }

    @Command
    public void back() {
        //Executions.sendRedirect("/ss");
        Executions.sendRedirect("/selectService.zul");
    }

    @Command
    @NotifyChange({"timeList", "date"})
    public void changeDate() {
        RpcGetGridOfWeek.GridAndParams gp;
        try {
            gp = NetCommander.getGridOfWeek(ServiceList.netProperty, client.getService().getId(), date, -1);
        } catch (Exception ex) {
            ex.printStackTrace();
            Clients.showNotification("Bad net conversation", Clients.NOTIFICATION_TYPE_ERROR, listbox, "middle_center", 5000, true);
            return;
            //throw new RuntimeException("Bad net conversation: " + ex);
        }

        timeList.clear();
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


}
