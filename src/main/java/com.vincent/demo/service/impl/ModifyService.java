package com.vincent.demo.service.impl;

import com.vincent.demo.service.IModifyService;
import com.vincent.spring.framework.annotation.VincentService;

/**
 * 增删改业务
 */
@VincentService
public class ModifyService implements IModifyService {

    @Override
    public String add(String name, String addr) {
        return "modifyService add, name= " + name + ",addr= " + addr;
    }

    @Override
    public String edit(Integer id, String name) {
        return "modifySerivce edit, id = " + id + ", name= " + name;
    }

    @Override
    public String remove(Integer id) {
        return "modifySerivce id= " + id;
    }
}
