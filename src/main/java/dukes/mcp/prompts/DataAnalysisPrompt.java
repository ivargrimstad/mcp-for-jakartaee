package dukes.mcp.prompts;

import dukes.mcp.model.PromptArgument;
import dukes.mcp.model.PromptMessage;
import dukes.mcp.service.Prompt;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

/**
 * Example prompt for data analysis scenarios.
 * 
 * <p>This prompt generates a data analysis request for AI assistants, allowing
 * specification of the data type and the analysis goal. The prompt helps structure
 * requests for analyzing datasets, metrics, or other data sources.</p>
 * 
 * <p>The prompt accepts two parameters:</p>
 * <ul>
 *   <li><b>data_type</b> (required): The type of data to analyze (e.g., "sales data", "user metrics", "log files")</li>
 *   <li><b>analysis_goal</b> (required): The goal or objective of the analysis (e.g., "identify trends", "find anomalies")</li>
 * </ul>
 * 
 * <p>Example usage through MCP protocol:</p>
 * <pre>{@code
 * {
 *   "jsonrpc": "2.0",
 *   "id": "1",
 *   "method": "prompts/get",
 *   "params": {
 *     "name": "data_analysis",
 *     "arguments": {
 *       "data_type": "sales data",
 *       "analysis_goal": "identify seasonal trends and predict next quarter revenue"
 *     }
 *   }
 * }
 * }</pre>
 * 
 * @see Prompt
 * @see PromptArgument
 * @see PromptMessage
 */
@ApplicationScoped
public class DataAnalysisPrompt implements Prompt {
    
    /**
     * Returns the unique identifier for this prompt.
     * 
     * @return "data_analysis"
     */
    @Override
    public String getName() {
        return "data_analysis";
    }
    
    /**
     * Returns a description of what this prompt does.
     * 
     * @return a description explaining the prompt's purpose
     */
    @Override
    public String getDescription() {
        return "Generates a prompt for analyzing data with a specific goal or objective";
    }
    
    /**
     * Returns the list of arguments this prompt accepts.
     * 
     * <p>Arguments:</p>
     * <ul>
     *   <li><b>data_type</b> (required): The type of data to analyze</li>
     *   <li><b>analysis_goal</b> (required): The goal or objective of the analysis</li>
     * </ul>
     * 
     * @return list of prompt arguments
     */
    @Override
    public List<PromptArgument> getArguments() {
        return List.of(
            new PromptArgument("data_type", "The type of data to analyze (e.g., sales data, user metrics, log files)", true),
            new PromptArgument("analysis_goal", "The goal or objective of the analysis (e.g., identify trends, find anomalies, predict outcomes)", true)
        );
    }
    
    /**
     * Renders the prompt by substituting argument values into the template.
     * 
     * <p>This method validates that both required arguments ("data_type" and "analysis_goal")
     * are provided, then generates a data analysis prompt with structured guidance for
     * the AI assistant.</p>
     * 
     * @param arguments map of argument names to values
     * @return list containing a single user prompt message with the rendered content
     * @throws IllegalArgumentException if any required argument is missing or empty
     * @throws NullPointerException if arguments is null
     */
    @Override
    public List<PromptMessage> render(Map<String, String> arguments) {
        if (arguments == null) {
            throw new NullPointerException("Arguments map cannot be null");
        }
        
        // Validate required arguments
        String dataType = arguments.get("data_type");
        if (dataType == null || dataType.trim().isEmpty()) {
            throw new IllegalArgumentException("Required argument 'data_type' is missing or empty");
        }
        
        String analysisGoal = arguments.get("analysis_goal");
        if (analysisGoal == null || analysisGoal.trim().isEmpty()) {
            throw new IllegalArgumentException("Required argument 'analysis_goal' is missing or empty");
        }
        
        // Build the prompt content
        String content = """
                Please analyze the following %s with the goal to: %s.

                In your analysis, please:
                - Examine the data structure and key characteristics
                - Identify patterns, trends, or anomalies relevant to the goal
                - Provide statistical insights where applicable
                - Highlight any data quality issues or limitations
                - Offer actionable recommendations based on the findings
                - Suggest visualizations that would help communicate the results

                Focus specifically on: %s""".formatted(dataType, analysisGoal, analysisGoal);

        return List.of(PromptMessage.user(content));
    }
}
