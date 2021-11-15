package eu.pledgerproject.confservice.scheduler;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class AppFilter {

	public static void main(String[] args) {
		ExpressionParser parser = new SpelExpressionParser();
		StandardEvaluationContext context = new StandardEvaluationContext();

		context.setVariable("black", "black");
		Object trueValue = parser.parseExpression("#black == 'black' and 1>2").getValue(context);
		System.out.println(trueValue);
	}
	
}
