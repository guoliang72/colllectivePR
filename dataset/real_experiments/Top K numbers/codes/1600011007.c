#include<stdio.h>
int main()
{
	int u[1002];
	int i,n,m,temp,k;
	scanf("%d",&n);
	for(i=0;i<n;i=i+1)
	{
		scanf("%d",u[i]);			// 输入错
	}
	// 没读入k
	for(i=n-1;i>0;i=i-1)				// 正确
	{
		for(m=0;m<i;m=m+1)			// 正确
		{
			if(u[m]<u[m+1])
			{
				temp=u[m];
				u[m]=u[m+1];
				u[m+1]=temp;
			}
		}
	}
	for(i=0;i<n;i=i+1)				// 输出边界错误，应为k
	{
	printf("%d\n",u[i]);
	}
	return 0;
}