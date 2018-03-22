/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.apertum.qsystem.prereg;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import ru.apertum.qsystem.common.Multilingual;

import java.util.ArrayList;
import java.util.Locale;

import static ru.apertum.qsystem.common.Multilingual.UA_LNG;

/**
 * @author Evgeniy Egorov
 */
public class Index {

    private Multilingual.Lng lang;

    public String l(String resName) {
        return Labels.getLabel(resName);
    }

    //*****************************************************
    //**** Multilingual
    //*****************************************************
    public ArrayList<Multilingual.Lng> getLangs() {
        return Multilingual.LANGS;
    }

    public Multilingual.Lng getLang() {
        return lang;
    }

    public void setLang(Multilingual.Lng lang) {
        this.lang = lang;
    }

    @Command("changeLang")
    public void changeLang() {
        if (lang != null) {
            initPreferredLocale();
            Executions.sendRedirect(null);
        }
    }

    @Init
    public void init() {
        lang = UA_LNG;
        initPreferredLocale();
        String lng = Executions.getCurrent().getParameter("locale") != null ? Executions.getCurrent().getParameter("locale") : "uk_UA";
        lang = new Multilingual().init(lng);
    }

    private void initPreferredLocale() {
        final org.zkoss.zk.ui.Session session = Sessions.getCurrent();
        final Locale prefer_locale = lang.code.length() > 2
                ? new Locale(lang.code.substring(0, 2), lang.code.substring(3)) : new Locale(lang.code);
        session.setAttribute(org.zkoss.web.Attributes.PREFERRED_LOCALE, prefer_locale);
    }

}
