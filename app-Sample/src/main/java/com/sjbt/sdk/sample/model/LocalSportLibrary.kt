package com.sjbt.sdk.sample.model

/**
 * Created by qiyachao
 * on 2023_10_21
 */
 class LocalSportLibrary{
     val sports= mutableListOf<LocalSport>()
    class LocalSport{
        val id =0
        var buildIn =false
        var installed =false
        val type =0
        val names = hashMapOf<String,String>()
    }
 }

