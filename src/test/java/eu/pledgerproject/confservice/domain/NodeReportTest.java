package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class NodeReportTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(NodeReport.class);
        NodeReport nodeReport1 = new NodeReport();
        nodeReport1.setId(1L);
        NodeReport nodeReport2 = new NodeReport();
        nodeReport2.setId(nodeReport1.getId());
        assertThat(nodeReport1).isEqualTo(nodeReport2);
        nodeReport2.setId(2L);
        assertThat(nodeReport1).isNotEqualTo(nodeReport2);
        nodeReport1.setId(null);
        assertThat(nodeReport1).isNotEqualTo(nodeReport2);
    }
}
