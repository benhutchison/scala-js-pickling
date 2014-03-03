package org.scalajs.spickling

import scala.language.experimental.macros

import scala.reflect.macros.Context

object PicklerMaterializersImpl {
  def materializePickler[T: c.WeakTypeTag](c: Context): c.Expr[Pickler[T]] = {
    import c.universe._

    val tpe = weakTypeOf[T]
    val sym = tpe.typeSymbol.asClass

    if (!sym.isCaseClass) {
      c.error(c.enclosingPosition,
          "Cannot materialize pickler for non-case class")
      return c.Expr[Pickler[T]](q"null")
    }

    val accessors = (tpe.declarations collect {
      case acc: MethodSymbol if acc.isCaseAccessor => acc
    }).toList

    val pickleFields = for {
      accessor <- accessors
    } yield {
      val fieldName = accessor.name
      val fieldString = fieldName.toString()
      q"""
        ($fieldString, (value.$fieldName.pickle))
      """
    }

    val pickleLogic = q"""
      builder.makeObject(..$pickleFields)
    """

    val result = q"""
      implicit object GenPickler extends org.scalajs.spickling.Pickler[$tpe] {
        import org.scalajs.spickling._
        def pickle[P](value: $tpe)(implicit builder: PBuilder[P]): P = $pickleLogic
      }
      GenPickler
    """

    c.Expr[Pickler[T]](result)
  }

  def materializeUnpickler[T: c.WeakTypeTag](c: Context): c.Expr[Unpickler[T]] = {
    import c.universe._

    val tpe = weakTypeOf[T]
    val sym = tpe.typeSymbol.asClass

    if (!sym.isCaseClass) {
      c.error(c.enclosingPosition,
          "Cannot materialize pickler for non-case class")
      return c.Expr[Unpickler[T]](q"null")
    }

    val accessors = (tpe.declarations collect {
      case acc: MethodSymbol if acc.isCaseAccessor => acc
    }).toList

    val unpickledFields = for {
      accessor <- accessors
    } yield {
      val fieldName = accessor.name
      val fieldString = fieldName.toString()
      val fieldTpe = accessor.returnType
      q"""
        registry.unpickle(reader.readObjectField(
            pickle, $fieldString)).asInstanceOf[$fieldTpe]
      """
    }

    val unpickleLogic = q"""
      new $tpe(..$unpickledFields)
    """

    val result = q"""
      implicit object GenUnpickler extends org.scalajs.spickling.Unpickler[$tpe] {
        import org.scalajs.spickling._
        override def unpickle[P](pickle: P)(
            implicit registry: PicklerRegistry,
      reader: PReader[P]): $tpe = $unpickleLogic
      }
      GenUnpickler
    """

    println(s"result: $result")

    c.Expr[Unpickler[T]](result)
  }

  def materializeCaseObjectName[T: c.WeakTypeTag](
      c: Context): c.Expr[PicklerRegistry.SingletonFullName[T]] = {
    import c.universe._

    val tpe = weakTypeOf[T]
    val sym = tpe.typeSymbol.asClass

    if (!sym.isModuleClass || !sym.isCaseClass)
      c.abort(c.enclosingPosition,
          s"Cannot generate a case object name for non-case object $sym")

    val name = sym.fullName+"$"
    val result = q"""
      new org.scalajs.spickling.PicklerRegistry.SingletonFullName($name)
    """

    c.Expr[PicklerRegistry.SingletonFullName[T]](result)
  }
}

trait PicklerMaterializers {
  implicit def materializePickler[T]: Pickler[T] =
    macro PicklerMaterializersImpl.materializePickler[T]

  implicit def materializeUnpickler[T]: Unpickler[T] =
    macro PicklerMaterializersImpl.materializeUnpickler[T]

  implicit def materializeCaseObjectName[T]: PicklerRegistry.SingletonFullName[T] =
    macro PicklerMaterializersImpl.materializeCaseObjectName[T]
}
