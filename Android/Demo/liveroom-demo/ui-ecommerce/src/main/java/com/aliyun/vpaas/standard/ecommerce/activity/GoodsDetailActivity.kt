package com.aliyun.vpaas.standard.ecommerce.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.aliyun.roompaas.uibase.util.immersionbar.ImmersionBar
import com.aliyun.vpaas.standard.ecommerce.R

class GoodsDetailActivity : AppCompatActivity() {

    companion object {
        fun open(context: Context) {
            context.startActivity(Intent(context, GoodsDetailActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this)
            .statusBarColor("#F2F2F2")
            .init()
        setContentView(R.layout.activity_goods_detail)
    }
}