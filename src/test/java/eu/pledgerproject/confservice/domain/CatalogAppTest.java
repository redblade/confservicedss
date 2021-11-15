package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class CatalogAppTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CatalogApp.class);
        CatalogApp catalogApp1 = new CatalogApp();
        catalogApp1.setId(1L);
        CatalogApp catalogApp2 = new CatalogApp();
        catalogApp2.setId(catalogApp1.getId());
        assertThat(catalogApp1).isEqualTo(catalogApp2);
        catalogApp2.setId(2L);
        assertThat(catalogApp1).isNotEqualTo(catalogApp2);
        catalogApp1.setId(null);
        assertThat(catalogApp1).isNotEqualTo(catalogApp2);
    }
}
