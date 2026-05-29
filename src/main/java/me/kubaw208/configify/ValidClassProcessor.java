package me.kubaw208.configify;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import me.kubaw208.configify.annotations.CheckClass;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.*;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ValidClassProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "ValidClassProcessor started");

        Trees trees = Trees.instance(processingEnv);

        for(Element root : roundEnv.getRootElements()) {
            TreePath rootPath = trees.getPath(root);

            if(rootPath == null) continue;

            new TreePathScanner<Void, Void>() {
                @Override
                public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
                    TreePath currentPath = getCurrentPath();
                    Element invoked = trees.getElement(currentPath);

                    if(!(invoked instanceof ExecutableElement execElem))
                        return super.visitMethodInvocation(node, unused);

                    AnnotationMirror checkMirror = findCheckMirror(execElem);

                    if(checkMirror == null)
                        return super.visitMethodInvocation(node, unused);

                    TypeMirror requiredAnnotationType = getAnnotationValue(checkMirror);

                    if(requiredAnnotationType == null)
                        return super.visitMethodInvocation(node, unused);

                    TypeElement requiredAnnotationElement = (TypeElement) processingEnv.getTypeUtils().asElement(requiredAnnotationType);

                    if(node.getArguments().isEmpty())
                        return super.visitMethodInvocation(node, unused);

                    ExpressionTree arg = node.getArguments().get(0);

                    if(!(arg instanceof MemberSelectTree memberSelect))
                        return super.visitMethodInvocation(node, unused);

                    TreePath classPath = new TreePath(currentPath, memberSelect.getExpression());
                    Element classElem = trees.getElement(classPath);

                    if(!(classElem instanceof TypeElement typeElem))
                        return super.visitMethodInvocation(node, unused);

                    if(!hasAnnotation(typeElem, requiredAnnotationElement)) {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.ERROR,
                                "Class " + typeElem.getQualifiedName()
                                        + " is missing @" + requiredAnnotationElement.getSimpleName()
                        );
                    }

                    return super.visitMethodInvocation(node, unused);
                }

                private AnnotationMirror findCheckMirror(ExecutableElement execElem) {
                    for(AnnotationMirror mirror : execElem.getAnnotationMirrors()) {
                        if(mirror.getAnnotationType().toString().equals(CheckClass.class.getName()))
                            return mirror;
                    }
                    return null;
                }

                private TypeMirror getAnnotationValue(AnnotationMirror mirror) {
                    Map<? extends ExecutableElement, ? extends AnnotationValue> values = processingEnv.getElementUtils().getElementValuesWithDefaults(mirror);

                    for(var entry : values.entrySet()) {
                        if(entry.getKey().getSimpleName().contentEquals("value"))
                            return (TypeMirror) entry.getValue().getValue();
                    }
                    return null;
                }

                private boolean hasAnnotation(TypeElement type, TypeElement required) {
                    for(AnnotationMirror ann : type.getAnnotationMirrors()) {
                        if(ann.getAnnotationType().asElement().equals(required)) return true;
                    }
                    return false;
                }
            }.scan(rootPath, null);
        }
        return false;
    }

}