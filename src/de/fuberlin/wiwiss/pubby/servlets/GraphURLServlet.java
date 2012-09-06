package de.fuberlin.wiwiss.pubby.servlets;

import com.hp.hpl.jena.rdf.model.Model;
import de.fuberlin.wiwiss.pubby.Configuration;
import de.fuberlin.wiwiss.pubby.MappedResource;
import de.fuberlin.wiwiss.pubby.ResourceDescription;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class GraphURLServlet extends BaseURLServlet {

    @Override
    protected boolean doGet(
            MappedResource resource, HttpServletRequest request, HttpServletResponse response, Configuration config
    ) throws IOException, ServletException {
        final Model graph = getGraphDescription(resource);

        if (graph.size() == 0) {
            return false;
        }

        Velocity.setProperty("velocimacro.context.localscope", Boolean.TRUE);

        ResourceDescription resourceDescription = new ResourceDescription(
                resource, graph, config);
        String discoLink = "http://www4.wiwiss.fu-berlin.de/rdf_browser/?browse_uri=" +
                URLEncoder.encode(resource.getWebURI(), "utf-8");
        String tabulatorLink = "http://dig.csail.mit.edu/2005/ajar/ajaw/tab.html?uri=" +
                URLEncoder.encode(resource.getWebURI(), "utf-8");
        String openLinkLink = "http://linkeddata.uriburner.com/ode/?uri=" +
                URLEncoder.encode(resource.getWebURI(), "utf-8");

        final List resources = resourceDescription.getResources();
        VelocityHelper template = new VelocityHelper(getServletContext(), response);
        Context context = template.getVelocityContext();

        context.put("project_name", config.getProjectName());
        context.put("project_link", config.getProjectLink());
        context.put("uri", resourceDescription.getURI());
        context.put("server_base", config.getWebApplicationBaseURI());

        context.put("rdf_link", resource.getDataURL());

        context.put("disco_link", discoLink);
        context.put("tabulator_link", tabulatorLink);
        context.put("openlink_link", openLinkLink);
        context.put("sparql_endpoint", resource.getDataset().getDataSource().getEndpointURL());

        context.put("title", resourceDescription.getLabel());
        context.put("comment", String.format("Found %d triples %d resources.", graph.size(), resources.size()));
        context.put("resources", resources);

        template.renderXHTML("graphpage.vm");
        return true;
    }

}
