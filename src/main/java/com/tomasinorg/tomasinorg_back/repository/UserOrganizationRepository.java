package com.tomasinorg.tomasinorg_back.repository;

import com.tomasinorg.tomasinorg_back.model.UserOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserOrganizationRepository extends JpaRepository<UserOrganization, Long> {
    
    List<UserOrganization> findByUserId(Long userId);
    
    List<UserOrganization> findByOrganizationId(Long organizationId);
    
    Optional<UserOrganization> findByUserIdAndOrganizationId(Long userId, Long organizationId);
    
    @Query("SELECT uo FROM UserOrganization uo WHERE uo.user.email = :email")
    List<UserOrganization> findByUserEmail(@Param("email") String email);
    
    @Query("SELECT uo FROM UserOrganization uo WHERE uo.organization.id = :orgId AND uo.user.email = :email")
    Optional<UserOrganization> findByOrganizationIdAndUserEmail(@Param("orgId") Long orgId, @Param("email") String email);
    
    @Query("SELECT COUNT(uo) > 0 FROM UserOrganization uo WHERE uo.organization.id = :orgId AND uo.user.id = :userId")
    boolean existsByOrganizationIdAndUserId(@Param("orgId") Long orgId, @Param("userId") Long userId);
    
    @Query("SELECT uo FROM UserOrganization uo WHERE uo.position.id = :positionId")
    List<UserOrganization> findByPositionId(@Param("positionId") Long positionId);
    
    void deleteByUserIdAndOrganizationId(Long userId, Long organizationId);
}
