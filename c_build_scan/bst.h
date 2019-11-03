
// binary tree implementation
// https://www.codesdope.com/blog/article/binary-search-tree-in-c/

// removed delete
// added info pointer


struct ImgTreeNode
{
	void*				info; // the image info
	struct ImgTreeNode* right_child; // right child
	struct ImgTreeNode* left_child; // left child
};

struct ImgTreeNode* tree_search(struct ImgTreeNode* root, void* x);
struct ImgTreeNode* tree_new_node(void* x);
struct ImgTreeNode* tree_insert(struct ImgTreeNode* root, void* x);
void tree_inorder(struct ImgTreeNode* root, void (*func)(void*));