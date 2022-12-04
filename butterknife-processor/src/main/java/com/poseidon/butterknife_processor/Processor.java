package com.poseidon.butterknife_processor;

import com.poseidon.butterknife_annotation.BindView;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

public class Processor extends AbstractProcessor {

    private ProcessingEnvironment mProcessingEnvironment;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mProcessingEnvironment = processingEnv;
    }

    //第一个参数为该注解处理器支持的所有类型
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        /**
         * 从所有元素中查找使用了支持注解的元素
         * 1.roundEnvironment.getRootElements() 获取所有的element对象
         * 2.getTypeElementsToProcess中匹配支持的所有元素
         */
        for (TypeElement typeElement : getTypeElementsToProcess(roundEnvironment.getRootElements(),set)){
            // 从类元素中获取包名类名
            String packageName = mProcessingEnvironment.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
            String typeName = typeElement.getSimpleName().toString();
            ClassName className = ClassName.get(packageName, typeName);

            // 构造生成的中间类名
            ClassName generatedClassName = ClassName.get(packageName, typeName + "ViewBinding");
            // 根据类名创建类构造器
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(generatedClassName).addModifiers(Modifier.PUBLIC);
            // 向类构造器中添加构造函数，构造函数的参数为类名，其内部调用bindViews函数
            classBuilder.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(className, "activity")
                    .addStatement("$N($N)",
                            "bindViews",
                            "activity")
                    .build());

            // 构造bindViews函数实现，其使用private修饰，返回值是void
            MethodSpec.Builder bindViewsMethodBuilder = MethodSpec
                    .methodBuilder("bindViews")
                    .addModifiers(Modifier.PRIVATE)
                    .returns(void.class)
                    .addParameter(className, "activity");

            // 获取类的所有成员属性
            for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
                BindView bindView = variableElement.getAnnotation(BindView.class);
                if (bindView != null) {
                    // 如果成员属性使用@BindView注解，就向bindViews函数内部加入一行
                    bindViewsMethodBuilder.addStatement("$N.$N = ($T) $N.findViewById($L)",
                            "activity",
                            variableElement.getSimpleName(),
                            variableElement,
                            "activity",
                            bindView.value());
                }
            }
            // 将bindViews函数加入类构造器
            classBuilder.addMethod(bindViewsMethodBuilder.build());

            // 使用JavaFile将定义的类构造器写入真实文件中
            try {
                JavaFile.builder(packageName,
                                classBuilder.build())
                        .build()
                        .writeTo(mProcessingEnvironment.getFiler());
            } catch (IOException e) {
                mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString(), typeElement);
            }
        }
        return true;
    }

    private Set<TypeElement> getTypeElementsToProcess(Set<? extends Element> elements,
                                                      Set<? extends TypeElement> supportedAnnotations) {
        Set<TypeElement> typeElements = new HashSet<>();
        for (Element element : elements) {
            // 如果该元素是类类型元素
            if (element instanceof TypeElement) {
                boolean found = false;
                // 获取该类内部所有的变量成员和方法
                for (Element subElement : element.getEnclosedElements()) {
                    // 获取变量成员和方法上所使用的注解
                    for (AnnotationMirror mirror : subElement.getAnnotationMirrors()) {
                        for (Element annotation : supportedAnnotations) {
                            // 如果使用的注解包含在我们支持的注解内，则收集起来
                            if (mirror.getAnnotationType().asElement().equals(annotation)) {
                                typeElements.add((TypeElement) element);
                                found = true;
                                break;
                            }
                        }
                        if (found) break;
                    }
                    if (found) break;
                }
            }
        }
        return typeElements;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> annotationType = new HashSet<>();
        annotationType.add(BindView.class.getCanonicalName());
        return annotationType;
    }
}