
/* Nome: Gabriela Ramos 		RA: 620360*/
package meta.cyanLang;

import java.util.ArrayList;
import ast.ASTVisitor;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import ast.MethodDec;
import ast.MethodSignature;
import ast.ObjectDec;
import ast.StatementFor;
import ast.StatementIf;
import ast.StatementWhile;
import javassist.bytecode.analysis.Type;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionProgramUnit_ati;
import meta.ICheckProgramUnit_ati3;
import meta.ICompiler_ati;
import meta.IInstanceVariableDec_ati;
import saci.Tuple2;

public class CyanMetaobjectAddComp extends CyanMetaobjectWithAt
		implements IActionProgramUnit_ati, ICheckProgramUnit_ati3 {
	int statement = 0;
	public CyanMetaobjectAddComp() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "addComp";
	}

	@Override
	public ArrayList<Tuple2<String, StringBuffer>> ati_codeToAddToPrototypes(
			ICompiler_ati compiler) {

		CyanMetaobjectWithAtAnnotation annot = this.getMetaobjectAnnotation();
		// ObjectDec é a classe da AST que representa protótipos que não são
		// interfaces
		if ( !(annot.getDeclaration() instanceof ObjectDec) ) {
			// erro !
			this.addError("Deveria ser um prototipo");
		}
		ObjectDec prototype = (ObjectDec) annot.getDeclaration();
		String prototypeName = prototype.getName();
		String methodName = "<1";
		ArrayList<MethodSignature> mList;
		MethodDec lessThanMethod = null;
		mList = ((ObjectDec) compiler.getProgramUnit())
				.searchMethodPrivateProtectedPublic(methodName);
		if ( mList != null && mList.size() > 0 ) {
			// há um método < declarado
			if ( mList.get(0).getParameterList().get(0).getType() != compiler
					.getProgramUnit() ) {
				// erro
				this.addError("Tipo de parâmetro errado para método <");
				return null;
			}
			else {
				lessThanMethod = mList.get(0).getMethod();
				methodName = "< " + prototype.getFullName() + " -> cyan.lang.Boolean";
				if (lessThanMethod.getMethodSignature().getFullNameWithReturnType(null).compareTo(methodName) != 0) {
					this.addError("Prototipo deve possuir um método func < T -> Boolean");
				}
				}
		}
		else {
			// não há <, sinalize um erro
			this.addError("Este protótipo deveria ter um método < ");
			return null;
		}
		StringBuffer out = new StringBuffer();

		out.append("    override\n");
		out.append("    func == Dyn other -> Boolean {\n");
		out.append("        type other\n");
		out.append("            case " + prototypeName + " proto {\n");
		out.append("                return ");
		ArrayList<IInstanceVariableDec_ati> ivList = compiler
				.getInstanceVariableList();
		if ( ivList == null || ivList.size() == 0 ) {
			this.addError("no iv");
			return null;
		}
		int size = ivList.size();
		for (IInstanceVariableDec_ati iv : ivList) {
			String getMethod = iv.getName();
			if ( getMethod.startsWith("_") ) {
				getMethod = getMethod.substring(1);
			}
			else {
				getMethod = "get" + Character.toUpperCase(getMethod.charAt(0))
						+ getMethod.substring(1);
			}
			out.append(" " + iv.getName() + " == proto " + getMethod);
			if ( --size > 0 ) {
				out.append(" && ");
			}
		}
		out.append("\n            }\n");
		out.append("            case Dyn d {\n");
		out.append("                return false\n");

		out.append("            }\n");
		out.append("    }\n");

		
		out.append("    func <= " + prototypeName + " other -> Boolean {\n");
		out.append("        return other == self || other < self;\n");
		out.append("    }\n");
	
		out.append("    func > " + prototypeName + " other -> Boolean {\n");
		out.append("        return other > self;\n");
		out.append("    }\n");
		
		out.append("    func >= " + prototypeName + " other -> Boolean {\n");
		out.append("        return other == self || other > self;\n");
		out.append("    }\n"); 
		ArrayList<Tuple2<String, StringBuffer>> array = new ArrayList<>();
		array.add(new Tuple2<String, StringBuffer>(
				annot.getPrototypeOfAnnotation(), out));
		return array;
	}

	@Override
	public DeclarationKind[] mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind[] decKindList = new DeclarationKind[] {
			DeclarationKind.INSTANCE_VARIABLE_DEC, DeclarationKind.METHOD_DEC,
			DeclarationKind.PROTOTYPE_DEC, DeclarationKind.PACKAGE_DEC,
			DeclarationKind.PROGRAM_DEC };

	@Override
	public void ati3_checkProgramUnit(ICompiler_ati compiler) {
		CyanMetaobjectWithAtAnnotation annot = this.getMetaobjectAnnotation();
		
		String methodName = "<1";
		ArrayList<MethodSignature> mList;
		MethodDec lessThanMethod = null;
		ObjectDec proto = (ObjectDec) annot.getDeclaration();
		mList = proto
				.searchMethodPrivateProtectedPublic(methodName);
		lessThanMethod = mList.get(0).getMethod();
		
		lessThanMethod.accept(new ASTVisitor() {
			@Override
			public void visit(StatementWhile node) {
				statement++;
				
			}
			@Override
			public void visit (StatementIf node) {
				statement++;
			}
			@Override
			public void visit (StatementFor node) {
				statement++;
			}
		});
		
		if (statement >= 1 ) {
			this.addError("Erro: não deve conter while, for ou if ");
		}

	}

}
