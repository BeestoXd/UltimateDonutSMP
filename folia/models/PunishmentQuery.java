package com.bx.ultimateDonutSmp.models;

public record PunishmentQuery(
        PunishmentType typeFilter,
        PunishmentFilterState stateFilter,
        PunishmentSortOrder sortOrder
) {
    public PunishmentQuery {
        stateFilter = stateFilter == null ? PunishmentFilterState.ALL : stateFilter;
        sortOrder = sortOrder == null ? PunishmentSortOrder.NEWEST : sortOrder;
    }

    public static PunishmentQuery defaultQuery() {
        return new PunishmentQuery(null, PunishmentFilterState.ALL, PunishmentSortOrder.NEWEST);
    }

    public PunishmentQuery nextTypeFilter() {
        return new PunishmentQuery(PunishmentType.nextFilter(typeFilter), stateFilter, sortOrder);
    }

    public PunishmentQuery nextStateFilter() {
        return new PunishmentQuery(typeFilter, stateFilter.next(), sortOrder);
    }

    public PunishmentQuery nextSortOrder() {
        return new PunishmentQuery(typeFilter, stateFilter, sortOrder.next());
    }
}
