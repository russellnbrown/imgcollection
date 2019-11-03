
// binary tree implementation
// https://www.codesdope.com/blog/article/binary-search-tree-in-c/

// removed delete
// added info pointer

#include "common.h"

struct ImgTreeNode* tree_search(struct ImgTreeNode* root, SetItemImage* _x)
{
	SetItemImage* x = (SetItemImage *)_x;
	if (root == NULL || ((SetItemImage *)root->info)->ihash == x->ihash) //if root->data is x then the element is found
		return root;
	else if (x->ihash > ((SetItemImage*)root->info)->ihash) // x is greater, so we will search the right subtree
		return tree_search(root->right_child, x);
	else //x is smaller than the data, so we will search the left subtree
		return tree_search(root->left_child, x);
}


//function to create a node
struct ImgTreeNode* tree_new_node(SetItemImage* x)
{
	struct ImgTreeNode* p;
	p = malloc(sizeof(struct ImgTreeNode));
	p->info = x;
	p->left_child = NULL;
	p->right_child = NULL;

	return p;
}

struct ImgTreeNode* tree_insert(struct ImgTreeNode* root, SetItemImage* x)
{
	//searching for the place to insert
	if (root == NULL)
		return tree_new_node(x);
	else if (x->ihash > ((SetItemImage*)root->info)->ihash) // x is greater. Should be inserted to right
		root->right_child = tree_insert(root->right_child, x);
	else // x is smaller should be inserted to left
		root->left_child = tree_insert(root->left_child, x);
	return root;
}


void tree_inorder(struct ImgTreeNode* root, void (*func)(void*))
{
	if (root != NULL) // checking if the root is not null
	{
		tree_inorder(root->left_child, func); // visiting left child
		func(root->info);
		tree_inorder(root->right_child, func);// visiting right child
	}
}