package com.auth.repos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.auth.entities.Otp;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
	List<Optional<Otp>> findByUsernameoremail(String usernameoremail);
	
	// access to only ADMIN, to periodically delete unused otps
	@Query("DELETE FROM Otp o WHERE o.expiresAt < :currentTime")
	void deleteExpiredOtps(@Param("currentTime") LocalDateTime currentTime);

}