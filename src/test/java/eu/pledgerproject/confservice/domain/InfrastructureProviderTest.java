package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class InfrastructureProviderTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(InfrastructureProvider.class);
        InfrastructureProvider infrastructureProvider1 = new InfrastructureProvider();
        infrastructureProvider1.setId(1L);
        InfrastructureProvider infrastructureProvider2 = new InfrastructureProvider();
        infrastructureProvider2.setId(infrastructureProvider1.getId());
        assertThat(infrastructureProvider1).isEqualTo(infrastructureProvider2);
        infrastructureProvider2.setId(2L);
        assertThat(infrastructureProvider1).isNotEqualTo(infrastructureProvider2);
        infrastructureProvider1.setId(null);
        assertThat(infrastructureProvider1).isNotEqualTo(infrastructureProvider2);
    }
}
