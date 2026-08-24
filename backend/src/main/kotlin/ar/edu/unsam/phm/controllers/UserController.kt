package ar.edu.unsam.phm.controllers

import ar.edu.unsam.phm.dto.user.EditUserDTO
import ar.edu.unsam.phm.dto.user.HeaderUserDTO
import ar.edu.unsam.phm.dto.user.NewUserRequestDTO
import ar.edu.unsam.phm.dto.user.UserDTO
import ar.edu.unsam.phm.services.AssembleService
import ar.edu.unsam.phm.services.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.access.prepost.PreAuthorize

@RestController
@RequestMapping("/api/user")
class UserController(
    val userService: UserService,
    val assembleService: AssembleService
) {
    @PostMapping
    fun createAccount(@RequestBody newUserDTO: NewUserRequestDTO): ResponseEntity<Void>{
        userService.createUser(newUserDTO)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('Lector','Publicador')")
    fun getUserById(@PathVariable userId: Int): UserDTO {
        return assembleService.getUserData(userId)
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('Lector','Publicador')")
    fun updateUserProfile(@PathVariable userId: Int, @RequestBody editUserDTO: EditUserDTO): ResponseEntity<Void> {
        assembleService.updateUser(userId, editUserDTO)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/header/{userId}")
    @PreAuthorize("hasAnyAuthority('Lector','Publicador')")
    fun getHeaderData(@PathVariable userId: Int): HeaderUserDTO {
        return userService.getHeaderData(userId)
    }
}