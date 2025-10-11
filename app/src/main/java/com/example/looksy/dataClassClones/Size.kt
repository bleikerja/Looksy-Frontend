package com.example.looksy.dataClassClones

enum class Size {  _34, _36, _38, _40, _42, _44, _46, _48, _50, _52, _54, _56, _58, _60, _XS, _S, _M, _L, _XL;
    fun  onlyLetters (size:Size) : Size{
        if (size == Size._34 || size == Size._36) {
            return _XS;
        }
        else if (size == Size._38) {
            return _S;
        }
        else if (size == Size._40) {
            return _M;
        }
        else if (size == Size._42) {
            return _L;
        }
        else if (size == Size._44) {
            return _XL;
        }
        else{
            throw NoKnownSize("I don't know which Size this is.")
        }
    }
}

class NoKnownSize(message: String?) : RuntimeException(message)
