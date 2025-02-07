package com.auth.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.auth.entities.Otp;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
	Optional<Otp> findByUsernameoremail(String usernameoremail);
}