package com.tomasinorg.tomasinorg_back.repository;

import com.tomasinorg.tomasinorg_back.model.OrganizationPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationPositionRepository extends JpaRepository<OrganizationPosition, Long> {
    
    List<OrganizationPosition> findByOrganizationId(Long organizationId);
    
    Optional<OrganizationPosition> findByOrganizationIdAndName(Long organizationId, String name);
    
    Optional<OrganizationPosition> findByOrganizationIdAndIsDefaultTrue(Long organizationId);
    
    @Query("SELECT COUNT(uo) FROM UserOrganization uo WHERE uo.position.id = :positionId")
    Long countUsersWithPosition(@Param("positionId") Long positionId);
    
    boolean existsByOrganizationIdAndName(Long organizationId, String name);
}
