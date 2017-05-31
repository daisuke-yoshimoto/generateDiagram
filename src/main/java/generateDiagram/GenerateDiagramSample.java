package generateDiagram;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;

public class GenerateDiagramSample {

	public static void main(String[] args) throws IOException {
		ProcessEngine processEngine = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration().
				setDatabaseSchemaUpdate("create-drop").
				setJdbcDriver("org.h2.Driver").setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000").
				setJdbcPassword("").setJdbcUsername("sa").buildProcessEngine();
		
		RepositoryService repositoryService = processEngine.getRepositoryService();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		TaskService taskService = processEngine.getTaskService();
		
		InputStream processDefinitionDiagram = null;
		InputStream processInstanceDiagram = null;
		try {
			Deployment deployment = repositoryService.createDeployment().addInputStream("MyProcess.bpmn20.xml",
					new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/diagrams/SampleProcess.bpmn")).deploy();
			
			ProcessDefinition processDef = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
			
			// Generate process definition diagram
			processDefinitionDiagram = repositoryService.getProcessDiagram(processDef.getId());
			File file = new File("./", "SampleProcess.png");
			Files.copy(processDefinitionDiagram, file.toPath());
			
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("sample_process");
			String taskId = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId();
			Map<String, Object> vars = new HashMap<String, Object>();
			vars.put("key", "aaa");
			taskService.complete(taskId, vars);
			
			// Generate process instance diagram
			BpmnModel bpmnModel = repositoryService.getBpmnModel(processDef.getId());
			ProcessDiagramGenerator diagramGenerator = processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator();
			processInstanceDiagram = diagramGenerator.generateDiagram(bpmnModel, "png", runtimeService.getActiveActivityIds(processInstance.getId()),
					Collections.<String>emptyList(), processEngine.getProcessEngineConfiguration().getActivityFontName(), processEngine.getProcessEngineConfiguration().getLabelFontName(),
					processEngine.getProcessEngineConfiguration().getAnnotationFontName(), processEngine.getProcessEngineConfiguration().getClassLoader(), 1.0);
			file = new File("./", processInstance.getId() + ".png");
			Files.copy(processInstanceDiagram, file.toPath());
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			if (processDefinitionDiagram != null) {
				processDefinitionDiagram.close();
			}
			if (processInstanceDiagram != null) {
				processInstanceDiagram.close();
			}
			processEngine.close();
		}
	}

}
