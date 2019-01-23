package com.bugtracking.server.domain.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private EntityManager em;

    public ProjectRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Page<Project> findAllByFilters(Pageable pageable) {
        if (pageable == null) {
            pageable = PageRequest.of(0, 1000);
        }

        StringBuilder queryBuilder = new StringBuilder("FROM Project p ");
        TypedQuery<Number> countQuery = em.createQuery("SELECT count(1) " + queryBuilder.toString(), Number.class);
        buildOrderQuery(queryBuilder, pageable);

        Query query = em.createQuery(queryBuilder.toString());
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Project> projectList = query.getResultList();
        long total = pageable.getOffset() + projectList.size();
        if (projectList.isEmpty() || projectList.size() == 0 || projectList.size() == pageable.getPageSize()) {
            total = countQuery.getSingleResult().longValue();
        }

        return new PageImpl(projectList, pageable, total);
    }

    private void buildOrderQuery(StringBuilder queryBuilder, Pageable pageable) {
        queryBuilder.append(" ORDER BY ");
        if (pageable.getSort() == null || !pageable.getSort().iterator().hasNext()) {
            queryBuilder.append(" p.name ASC, p.id ASC");
        } else {
            boolean needComa = false;
            boolean sortedById = false;
            for (Sort.Order order : pageable.getSort()) {
                if (needComa) {
                    queryBuilder.append(", ");
                }
                queryBuilder.append(" p.").append(order.getProperty()).append(' ').append(order.getDirection());
                needComa = true;
                if ("id".equalsIgnoreCase(order.getProperty())) {
                    sortedById = true;
                }
            }
            if (!sortedById) {
                if (needComa) {
                    queryBuilder.append(", ");
                }
                queryBuilder.append("p.id ASC");
            }
        }
    }
}
