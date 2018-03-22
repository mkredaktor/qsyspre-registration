/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.apertum.qsystem.prereg.core;

import org.zkoss.zul.AbstractTreeModel;
import ru.apertum.qsystem.server.model.QService;

/**
 *
 * @author Evgeniy Egorov
 */
public class TreeServices extends AbstractTreeModel<QService> {

    public TreeServices(QService root) {
        super(root);
    }
    
    @Override
    public boolean isLeaf(QService e) {
        return e.isLeaf();
    }

    @Override
    public QService getChild(QService e, int i) {
        return e.getChildAt(i);
    }

    @Override
    public int getChildCount(QService e) {
        return e.getChildCount();
    }

}
