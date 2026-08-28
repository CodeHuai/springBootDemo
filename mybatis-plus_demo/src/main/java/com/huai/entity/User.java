package com.huai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;

@Data
@TableName("user")
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private int age;

    private int gender;
}
