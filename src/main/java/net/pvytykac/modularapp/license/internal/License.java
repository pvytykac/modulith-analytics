package net.pvytykac.modularapp.license.internal;

import net.pvytykac.modularapp.license.api.LicensePriceInfo;

import java.math.BigDecimal;

record License(String id, String name, BigDecimal monthlyPrice) implements LicensePriceInfo {
}
