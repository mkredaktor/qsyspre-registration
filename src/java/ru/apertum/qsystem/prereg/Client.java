package ru.apertum.qsystem.prereg;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.model.QAdvanceCustomer;
import ru.apertum.qsystem.server.model.QService;

public class Client {

    private String name;
    private String sourname;
    private String middlename = "";
    private String email;
    private String inputData;
    private QAdvanceCustomer advClient;

    public QAdvanceCustomer getAdvClient() {
        return advClient;
    }

    public void setAdvClient(QAdvanceCustomer advClient) {
        this.advClient = advClient;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourname() {
        return sourname;
    }

    public void setSourname(String sourname) {
        this.sourname = sourname;
    }
    private QService service;

    public QService getService() {
        return service;
    }

    public void setService(QService service) {
        this.service = service;
    }
    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        
        //day = format_dd_MM.format(date);
        day = format_dd_MM.format(date);
        finishT = format_HH_mm.format(date);
        this.date = date;
    }
    /**
     * Формат даты.
     */
    public final static DateFormat format_dd_MM = new SimpleDateFormat("dd MMMM");
    public final static DateFormat format_HH_mm = new SimpleDateFormat("HH.mm");
    private String day;
    private String finishT;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getFinishT() {
        return finishT;
    }

    public void setFinishT(String finishT) {
        this.finishT = finishT;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
         sb.append(sourname);
         sb.append(" ").append(name);
         if(middlename != null && !middlename.isEmpty()) {
             sb.append(" ").append(middlename);
         }
         return sb.toString();
    }
}
