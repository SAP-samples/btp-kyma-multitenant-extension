package dev.kyma.samples.easyfranchise.s4entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.kyma.samples.easyfranchise.dbentities.Tenant;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

class A_BusinessPartnerTest {

    @Test
    void testFromS4JsonToA_BusinessPartne() throws IOException {

        String json = new String(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("A_BusinessPartner-sample01.json").readAllBytes());

        A_BusinessPartner businessPartners = JsonbBuilder.create().fromJson(json, A_BusinessPartner.class);

        assertEquals(2, businessPartners.d.results.size());

        BusinessPartner bupa = businessPartners.d.results.get(0);
        assertEquals("10100001", bupa.BusinessPartner);
        assertEquals("Inlandskunde DE 1", bupa.BusinessPartnerFullName);
        assertEquals("/Date(1588723200000)/", bupa.CreationDate);
        assertEquals("BP03", bupa.BusinessPartnerGrouping);

        BusinessPartnerAddress adr1 = bupa.to_BusinessPartnerAddress.results.get(0);
        assertEquals("74214", adr1.PostalCode);
        assertEquals("Schoental", adr1.CityName);
        assertEquals("Lindenstrasse", adr1.StreetName);
        assertEquals("2", adr1.HouseNumber);

        EmailAddress email1 = adr1.to_EmailAddress.results.get(0);
        assertEquals("info@10100001.com", email1.EmailAddress);
    }

    @Test
    @SuppressWarnings("serial")
    void testFromJsonToTenant() throws IOException {

        String json = "[{\"password\":\"mySAP123\",\"schema\":\"cityscooter01\",\"tenantid\":\"344324-4242423-42423-23423\"},{\"password\":\"mySAP123\",\"schema\":\"cityscooter\",\"tenantid\":\"28df3207-affd-4ac5-ae1f-921df5b415f5\"}]";

        Jsonb jsonb = JsonbBuilder.create();
        List<Tenant> tenants = jsonb.fromJson(json, new ArrayList<Tenant>() {
        }.getClass().getGenericSuperclass());
        System.out.println(tenants);
    }
}
