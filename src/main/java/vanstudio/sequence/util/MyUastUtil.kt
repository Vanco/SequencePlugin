package vanstudio.sequence.util

import org.jetbrains.uast.*
import vanstudio.sequence.diagram.Info
import vanstudio.sequence.openapi.IGenerator.ParamPair
import vanstudio.sequence.openapi.model.ClassDescription
import vanstudio.sequence.openapi.model.LambdaExprDescription
import vanstudio.sequence.openapi.model.MethodDescription

fun createMethod(node: ULambdaExpression, offset: Int): MethodDescription {
    val paramPair: ParamPair = extractParameters(node.valueParameters)
    val returnType = node.getExpressionType()?.canonicalText

    val uMethod = node.getParentOfType(UMethod::class.java, true)
    val enclosedMethod = createMethod(uMethod!!, offset)

    return LambdaExprDescription(enclosedMethod, returnType, paramPair.argNames, paramPair.argTypes, offset)
}

fun createMethod(node: UMethod, offset: Int): MethodDescription {
    val paramPair: ParamPair = extractParameters(node.uastParameters)
    val containingUClass = node.getContainingUClass()
    val attributes = createAttributes(node)

    if (node.isConstructor) {
        return MethodDescription.createConstructorDescription(
            createClassDescription(containingUClass),
            attributes, paramPair.argNames, paramPair.argTypes, offset
        )
    }

    val returnType = node.returnType
    return MethodDescription.createMethodDescription(
        createClassDescription(containingUClass),
        attributes, node.name, returnType?.canonicalText,
        paramPair.argNames, paramPair.argTypes, offset
    )
}

fun createClassDescription(containingUClass: UClass?): ClassDescription? {
    return ClassDescription(
        containingUClass?.qualifiedName,
        createAttributes(containingUClass)
    )
}

fun createAttributes(node: UClass?): List<String> {
    val attributes = ArrayList<String>()
    for (attribute in Info.RECOGNIZED_METHOD_ATTRIBUTES) {
        if (node?.hasModifierProperty(attribute) == true) {
            attributes.add(attribute)
        }
    }
    if (isExternal(node)) {
        attributes.add(Info.EXTERNAL_ATTRIBUTE);
    }
    if (isInterface(node)) {
        attributes.add(Info.INTERFACE_ATTRIBUTE)
    }
    return attributes
}


fun createAttributes(node: UMethod): List<String> {
    val attributes = ArrayList<String>()
    for (attribute in Info.RECOGNIZED_METHOD_ATTRIBUTES) {
        if (node.hasModifierProperty(attribute)) {
            attributes.add(attribute)
        }
    }
    val containingUClass = node.getContainingUClass()
    if (isExternal(containingUClass)) {
        attributes.add(Info.EXTERNAL_ATTRIBUTE);
    }
    if (isInterface(containingUClass)) {
        attributes.add(Info.INTERFACE_ATTRIBUTE)
    }
    return attributes
}

fun extractParameters(uastParameters: List<UParameter>): ParamPair {
    val argNames = ArrayList<String>()
    val argTypes = ArrayList<String>()
    for (uastParameter in uastParameters) {
        argNames.add(uastParameter.name)
        argTypes.add(uastParameter.type.canonicalText)
    }
    return ParamPair(argNames, argTypes)
}

fun isExternal(uClass: UClass?): Boolean {
    val virtualFile = uClass?.containingFile?.virtualFile
    val protocol = virtualFile?.fileSystem?.protocol

    return virtualFile?.name?.endsWith(".class") == true
            || protocol?.equals("jar", ignoreCase = true) == true
            || protocol?.equals("zip", ignoreCase = true) == true
}

fun isInterface(uClass: UClass?): Boolean {
    return uClass?.isInterface ?: false
}
