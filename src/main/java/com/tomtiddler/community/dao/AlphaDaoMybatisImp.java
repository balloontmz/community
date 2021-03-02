package com.tomtiddler.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class AlphaDaoMybatisImp implements  AlphaDao{
    @Override
    public String select() {
        return "MyBatis";
    }
}
