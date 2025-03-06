<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>코비하우스VR 관리자페이지</title>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/jstree@3.3.12/dist/themes/default/style.min.css">
    <script type="text/javascript" src="<c:url value='/resources/addon/jquery/js/jquery-3.3.1.min.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/resources/addon/jstree/js/jstree.min.js'/>"></script>

    <script type="text/javascript">
        $(document).ready(function() {
            fn_ajaxGetServerDirectoryTree();
        });

        function fn_ajaxGetServerDirectoryTree() {
            $("#popupTree").jstree("destroy");

            $('#popupTree').jstree({
                'plugins': ["search", "wholerow"],
                'core': {
                    "themes": {
                        "name": "default",
                        "dots": true,
                        "icons": true
                    },
                    'check_callback': true,
                    'data': function (node, callback) {
                        if (node.id === "#") {
                            $.ajax({
                                type: "POST",
                                url: "<c:url value='/updateMgt/ajaxGetServerDirectoryTree.do'/>",
                                dataType: "json",
                                success: function (data) {
                                    callback(data.treeList);
                                },
                                error: function () {
                                    alert("최상위 폴더 로딩 실패");
                                }
                            });
                        } else {
                            $.ajax({
                                type: "POST",
                                url: "<c:url value='/updateMgt/ajaxGetFolderChildren.do'/>",
                                data: { parentPath: node.id },
                                dataType: "json",
                                success: function (data) {
                                    callback(data.treeList);
                                },
                                error: function () {
                                    alert("하위 폴더 로딩 실패");
                                }
                            });
                        }
                    }
                }
            });
        }
    </script>
</head>
<body>
<div id="popupTree"></div>
</body>
</html>
