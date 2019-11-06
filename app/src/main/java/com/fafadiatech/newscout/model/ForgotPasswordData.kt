package com.fafadiatech.newscout.model

data class ForgotPasswordData(var header: NewsStatus, var body: MessageBody? = null, var errors: MessageBody? = null)