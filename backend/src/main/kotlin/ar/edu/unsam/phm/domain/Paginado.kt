package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.exceptions.BadRequestException

object Paginado {
    fun validate(page: Int, size: Int) {
        if(page == null) throw BadRequestException("El atributo page es obligatorio")
        if(size == null) throw BadRequestException("El atributo size es obligatorio")
        if(page < 0) throw BadRequestException("El número de página no puede ser negativo")
        if(size <= 0) throw BadRequestException("El atributo size debe ser mayor a cero")
    }
}