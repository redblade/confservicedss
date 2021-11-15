package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class AppConstraintTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AppConstraint.class);
        AppConstraint appConstraint1 = new AppConstraint();
        appConstraint1.setId(1L);
        AppConstraint appConstraint2 = new AppConstraint();
        appConstraint2.setId(appConstraint1.getId());
        assertThat(appConstraint1).isEqualTo(appConstraint2);
        appConstraint2.setId(2L);
        assertThat(appConstraint1).isNotEqualTo(appConstraint2);
        appConstraint1.setId(null);
        assertThat(appConstraint1).isNotEqualTo(appConstraint2);
    }
}
