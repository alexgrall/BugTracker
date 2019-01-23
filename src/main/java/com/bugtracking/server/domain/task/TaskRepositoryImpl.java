package com.bugtracking.server.domain.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TaskRepositoryImpl implements TaskRepositoryCustom {

    private EntityManager em;

    public TaskRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Page<Task> findAllByFilters(Long projectId, Set<TaskStatus> statusSet, Set<Integer> prioritySet,
                                       LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (pageable == null) {
            pageable = PageRequest.of(0, 1000);
        }

        StringBuilder where = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        buildWhereQuery(where, params, projectId, statusSet, prioritySet, startDate, endDate);

        StringBuilder queryBuilder = new StringBuilder("FROM Task t ");
        if (params.size() > 0) {
            queryBuilder.append("WHERE ").append(where);
        }

        TypedQuery<Number> countQuery = em.createQuery("SELECT count(1) " + queryBuilder.toString(), Number.class);
        buildOrderQuery(queryBuilder, pageable);

        Query query = em.createQuery(queryBuilder.toString());
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        params.forEach((k, v) -> {
            query.setParameter(k, v);
            countQuery.setParameter(k, v);
        });

        @SuppressWarnings("unchecked")
        List<Task> taskList = query.getResultList();
        long total = pageable.getOffset() + taskList.size();
        if (taskList.isEmpty() || taskList.size() == 0 || taskList.size() == pageable.getPageSize()) {
            total = countQuery.getSingleResult().longValue();
        }

        return new PageImpl(taskList, pageable, total);
    }

    private void buildWhereQuery(StringBuilder where, Map<String, Object> params,
                                 Long projectId, Set<TaskStatus> statusSet, Set<Integer> prioritySet,
                                 LocalDateTime startDate, LocalDateTime endDate) {
        if (projectId != null) {
            where.append("t.projectId = :projectId");
            params.put("projectId", projectId);
        }
        if (statusSet != null && !statusSet.isEmpty() && statusSet.size() != TaskStatus.values().length) {
            if (params.size() > 0) {
                where.append(" AND ");
            }
            where.append('(');
            int i = 0;
            for (TaskStatus status : statusSet) {
                String paramName = "status" + i;
                where.append("t.status = :").append(paramName);
                params.put(paramName, status);
                ++i;
                if (i != statusSet.size()) {
                    where.append(" OR ");
                }
            }
            where.append(')');
        }
        if (prioritySet != null && !prioritySet.isEmpty()) {
            if (params.size() > 0) {
                where.append(" AND ");
            }
            where.append('(');
            int i = 0;
            for (Integer priority : prioritySet) {
                String paramName = "priority" + i;
                where.append("t.priority = :").append(paramName);
                params.put(paramName, priority);
                ++i;
                if (i != prioritySet.size()) {
                    where.append(" OR ");
                }
            }
            where.append(')');
        }
        if (startDate != null) {
            if (params.size() > 0) {
                where.append(" AND ");
            }
            where.append("t.modifiedAt >= :startDate");
            params.put("startDate", startDate);
        }
        if (endDate != null) {
            if (params.size() > 0) {
                where.append(" AND ");
            }
            where.append("t.modifiedAt <= :endDate");
            params.put("endDate", endDate);
        }
    }

    private void buildOrderQuery(StringBuilder queryBuilder, Pageable pageable) {
        queryBuilder.append(" ORDER BY ");
        if (pageable.getSort() == null || !pageable.getSort().iterator().hasNext()) {
            queryBuilder.append(" t.id ASC");
        } else {
            boolean needComa = false;
            boolean sortedById = false;
            for (Sort.Order order : pageable.getSort()) {
                if (needComa) {
                    queryBuilder.append(", ");
                }
                queryBuilder.append(" t.").append(order.getProperty()).append(' ').append(order.getDirection());
                needComa = true;
                if ("id".equalsIgnoreCase(order.getProperty())) {
                    sortedById = true;
                }
            }
            if (!sortedById) {
                if (needComa) {
                    queryBuilder.append(", ");
                }
                queryBuilder.append("t.id ASC");
            }
        }
    }
}
