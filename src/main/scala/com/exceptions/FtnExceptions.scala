package com.exceptions

sealed trait FtnException

class TokenValidationException(msg: String) extends RuntimeException(s"Json Validation Exception: $msg") with FtnException
class JsonValidation(msg: String) extends RuntimeException(msg) with FtnException

