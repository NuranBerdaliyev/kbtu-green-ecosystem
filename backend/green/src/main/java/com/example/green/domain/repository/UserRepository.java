package com.example.green.domain.repository;
import com.example.green.domain.entity.User;
import com.example.green.domain.enums.Role;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    Page<User> findByRoleIn(Collection<Role> roles, Pageable pageable);
    long countByRole(Role role);


    @Query("""
            select count(u) + 1
            from User u
            where u.role in :roles
              and (
                   u.esgRating > :esg
                   or (
                       u.esgRating = :esg
                       and u.totalCo2Saved > :co2
                   )
                   or (
                       u.esgRating = :esg
                       and u.totalCo2Saved = :co2
                       and u.id < :userId
                   )
              )
            """)
    long calculateRank(
            @Param("roles") Collection<Role> roles,
            @Param("userId") Long userId,
            @Param("esg") Integer esg,
            @Param("co2") BigDecimal co2
    );
}
