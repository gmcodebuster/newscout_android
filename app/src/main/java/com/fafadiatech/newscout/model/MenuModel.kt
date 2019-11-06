package com.fafadiatech.newscout.model

data class MenuModel(var menuName: String, var hasChildren: Boolean, var isGroup: Boolean, var subMenuData: SubMenuResultData?)

data class TopMenuModel(var menuName: String, var hasChildren: Boolean, var isGroup: Boolean, var topMenu: MenuHeading)