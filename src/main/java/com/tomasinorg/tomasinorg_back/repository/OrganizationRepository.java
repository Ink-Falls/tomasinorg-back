package com.tomasinorg.tomasinorg_back.repository;

import com.tomasinorg.tomasinorg_back.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    
    Optional<Organization> findByName(String name);
    
    @Query("SELECT DISTINCT o FROM Organization o JOIN o.userOrganizations uo WHERE uo.user.id = :userId")
    List<Organization> findByMembersId(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT o FROM Organization o JOIN o.userOrganizations uo WHERE uo.user.email = :email")
    List<Organization> findByMembersEmail(@Param("email") String email);
    
    @Query("SELECT COUNT(uo) > 0 FROM UserOrganization uo WHERE uo.organization.id = :orgId AND uo.user.id = :userId")
    boolean existsByIdAndMembersId(@Param("orgId") Long orgId, @Param("userId") Long userId);
}
