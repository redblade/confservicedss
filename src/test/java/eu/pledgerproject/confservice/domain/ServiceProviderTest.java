package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class ServiceProviderTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ServiceProvider.class);
        ServiceProvider serviceProvider1 = new ServiceProvider();
        serviceProvider1.setId(1L);
        ServiceProvider serviceProvider2 = new ServiceProvider();
        serviceProvider2.setId(serviceProvider1.getId());
        assertThat(serviceProvider1).isEqualTo(serviceProvider2);
        serviceProvider2.setId(2L);
        assertThat(serviceProvider1).isNotEqualTo(serviceProvider2);
        serviceProvider1.setId(null);
        assertThat(serviceProvider1).isNotEqualTo(serviceProvider2);
    }
}
