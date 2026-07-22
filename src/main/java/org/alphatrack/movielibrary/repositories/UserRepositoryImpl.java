package org.alphatrack.movielibrary.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.alphatrack.movielibrary.dtos.filters.UserFilterOptions;
import org.alphatrack.movielibrary.models.User;
import org.alphatrack.movielibrary.repositories.contracts.UserRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final EntityManager entityManager;

    @Autowired
    public UserRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<User> findAll(UserFilterOptions userFilterOptions) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> userRoot = cq.from(User.class);

        List<Predicate> predicates = new ArrayList<>();

        userFilterOptions.getUsername().ifPresent(username ->
                predicates.add(cb.like(userRoot.get("username"),"%" + username + "%")));

        userFilterOptions.getFirstName().ifPresent(firstName ->
                predicates.add(cb.like(userRoot.get("firstName"), "%" + firstName + "%")));

        userFilterOptions.getLastName().ifPresent(lastName ->
                predicates.add(cb.like(userRoot.get("lastName"), "%" + lastName + "%")));

        userFilterOptions.getEmail().ifPresent(email ->
                predicates.add(cb.like(userRoot.get("email"), "%" + email + "%")));

        cq.where(cb.and(predicates.toArray(new Predicate[0])));

        userFilterOptions.getSortBy().ifPresent(sortBy -> {
            String sortOrder = userFilterOptions.getSortOrder().orElse("asc");
            if (sortOrder.equalsIgnoreCase("desc")) {
                cq.orderBy(cb.desc(userRoot.get((sortBy))));
            } else {
                cq.orderBy(cb.asc(userRoot.get(sortBy)));
            }
        });

        return entityManager.createQuery(cq).getResultList();
    }
}
