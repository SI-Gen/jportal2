package bbd.jportal2.test;

import bbd.jportal2.*;
import org.junit.Assert;
import org.junit.Test;


/**
 * Created by dieter on 2017/05/30.
 * Unittest to test the FreeMarker template based generator
 */
public class AllTemplateBasedSIProcessorsBasicTest extends TestTemplateBasedProcessorsBaseClass<ITemplateBasedSIProcessor> {
    private final String OUTPUT_DIR = "target/test-all-generators-basic-test-out/";
    private final String SI_PROCESSOR_TEMPLATE_DIR = ITemplateBasedSIProcessor.SI_PROCESSOR_TEMPLATE_LOCATION;

    public AllTemplateBasedSIProcessorsBasicTest() {
        super(ITemplateBasedSIProcessor.class);
    }

    @Test
    public void testAllTemplateBasedSIProcessors() throws Exception {
        int success = this.testAllTemplateBasedProcessorsOfType(SI_PROCESSOR_TEMPLATE_DIR, OUTPUT_DIR);
        Assert.assertEquals(0, success);
    }


}

